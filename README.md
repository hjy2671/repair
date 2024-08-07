<h1 style="text-align: center">报修系统（毕业设计）</h1>
<div style="text-align: center">
</div>

#### 说明：
讨论＋v: w2671029778

#### 项目简介
基于el-admin开源框架 （前后端分离项目）

**el-admin官网：** https://el-admin.vip/

**主要技术：**

后端：springboot

前端：vue

数据库：mysql

中间件：redis

####  系统功能
- 1.前台页面进行响应式开发，页面大小改变时布局也会改变，可以适配pc端移动端

- 2.所有用户都可以进行登录，注册，找回密码，记住密码，自动登录

- 3.登录具备图形验证码校验，注册，找回密码需要手机验证码

- 4.菜单栏按登录用户角色显示不同菜单栏，如果不具备访问某个页面所需的角色，即使通过url也无法访问

- 4.首页，可以查看报修信息做一个统计，可查看全部报修信息，可以点赞，踩，查看当前报修信息提供人给出的评分

- 5.个人账户，展示昵称，性别，手机号，头像，累计获得赞踩数量，按角色显示累计提供，累计维修，累计指派，可以修改头像，昵称，性别，手机号

- 6.由我提供，展示我累计提供数量，显示由我提供的正在处理的报修任务，便于实时查看进度，未接受未指派的任务可以直接删除，否则不行

- 7.由我解决，显示我累计解决的数量，显示我累计解决的列表

- 8.由我指派，显示我累计指派的数量，显示我指派的未完成的报修任务便于实时查看报修进度

- 9.待处理任务，显示所有用户提交的等待指派的报修任务，等待管理员指派

- 10.指派给我，由管理员分配给我的报修任务，可以接受可拒绝，点击完成后不在此页显示

- 11.报修，填写位置信息，故障描述，选择紧急程度，可以携带三张图片，进行提交，提交后应当对改报修任务进行检查，判断当前所有未完成的任务，是否存在相似的，相似度大于60%的，会返回，在此页面最后显示列表，用户可以一一查看，判断是不是有别人提交过了，确认无误后再次确认提交

- 12.点击头像可进入后台页面，角色权限不足的则无法进入，也不会显示此按钮

#### 注意事项

- 项目中手机验证等调用阿里云的短信服务平台需要自行去阿里云官网开通服务，（有能力者可自行改成邮箱验证）
- 后端代码为完整代码，需要前端看项目说明

#### 图片预览

https://gitee.com/hjy267/project/tree/master/

前往gitee查看
