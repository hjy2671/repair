/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.security.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.config.bean.LoginProperties;
import me.zhengjie.modules.security.service.dto.JwtUserDto;
import me.zhengjie.modules.system.domain.RolePath;
import me.zhengjie.modules.system.service.RoleService;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.RoleSmallDto;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.RolePathService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@RequiredArgsConstructor
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;
    private final RoleService roleService;
    private final LoginProperties loginProperties;
    private final RolePathService rolePathService;

    public void setEnableCache(boolean enableCache) {
        this.loginProperties.setCacheEnable(enableCache);
    }

    /**
     * 用户信息缓存
     *
     * @see {@link UserCacheClean}
     */

    final static Map<String, Future<JwtUserDto>> USER_DTO_CACHE = new ConcurrentHashMap<>();
    public static ExecutorService executor = newThreadPool();

    @Override
    public JwtUserDto loadUserByUsername(String username) {
        JwtUserDto jwtUserDto = null;
        Future<JwtUserDto> future = USER_DTO_CACHE.get(username);

        if (!loginProperties.isCacheEnable()) {
            UserDto user;
            try {
                user = userService.findByName(username);
            } catch (EntityNotFoundException e) {
                // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
                throw new UsernameNotFoundException("", e);
            }
            if (user == null) {
                throw new UsernameNotFoundException("");
            } else {
                if (!user.getEnabled()) {
                    throw new BadRequestException("账号未激活！");
                }
                jwtUserDto = new JwtUserDto(
                        user,
                        roleService.mapToGrantedAuthorities(user)
                );
            }
            return jwtUserDto;
        }

        if (future==null) {
            Callable<JwtUserDto> call=()->getJwtBySearchDb(username);
            FutureTask<JwtUserDto> ft=new FutureTask<>(call);
            future=USER_DTO_CACHE.putIfAbsent(username,ft);
            if(future==null){
                future=ft;
                executor.submit(ft);
            }
            try{
                return future.get();
            }catch(CancellationException e){
                USER_DTO_CACHE.remove(username);
            }catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage());
            }
        }else{
            try {
                jwtUserDto=future.get();
                final UserDto user = jwtUserDto.getUser();
                if (user.getRolePathList() == null) {
                    final Set<Long> collect = user
                            .getRoles()
                            .stream()
                            .map(RoleSmallDto::getId)
                            .collect(Collectors.toSet());
                    final List<RolePath> list = rolePathService.list(
                            new QueryWrapper<RolePath>().select("distinct path").in("role_id", collect));
                    user.setRolePathList(list);
                }
            }catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return jwtUserDto;

    }

    private JwtUserDto getJwtBySearchDb(String username) {
        UserDto user;
        try {
            user = userService.findByName(username);
        } catch (EntityNotFoundException e) {
            // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
            throw new UsernameNotFoundException("", e);
        }
        if (user == null) {
            throw new UsernameNotFoundException("");
        } else {
            if (!user.getEnabled()) {
                throw new BadRequestException("账号未激活！");
            }
            return new JwtUserDto(
                    user,
                    roleService.mapToGrantedAuthorities(user)
            );
        }

    }

    public static ExecutorService newThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            final AtomicInteger sequence = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                int seq = this.sequence.getAndIncrement();
                thread.setName("future-task-thread" + (seq > 1 ? "-" + seq : ""));
                if (!thread.isDaemon()) {
                    thread.setDaemon(true);
                }

                return thread;
            }
        };
        return new ThreadPoolExecutor(10, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }
}
