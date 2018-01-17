### 记录对FBO的理解
* FBO:Frame Buffer Object,网上对FBO的讲解很多，这里主要记录一下个人对FBO的理解。顾名思义，帧缓存对象，就是一个缓存。理论不是本文的重点，理论自行去找，我直接说怎么使用吧。
* 以给图片添加滤镜为例，可以做滤镜链，这样一个图就可以过多个滤镜了。
* 使用FBO的步骤
    * 创建 glGenFramebuffers()
    * 绑定 glBindFramebuffer()
    * (这里使用绑定texture2D), glFramebufferTexture2D()把一幅纹理图像关联到一个FBO
    * 检查FBO状态 glCheckFramebufferStatus()
* 事例：
    * 需求：一张图片，过两个滤镜，显示最后的结果
    * 分析：有两种方法，方案一是图片生成bitmap,过一个滤镜，然后读取bitmap,再过另外一个滤镜；方案二是使用FBO，滤镜与滤镜之间通过TextureId传递，也就是说第一个滤镜的输出是第二个滤镜的输入，这个就可以形成一个滤镜串。
    * 实现
    
         ![实现效果](https://github.com/CL-window/FBO_demo/blob/master/image/image1.png)
         ![两种方式时间对比](https://github.com/CL-window/FBO_demo/blob/master/image/image2.png)
* 总结：个人理解，FBO作为一个缓冲区的存在，等于是openGL 绘制的结果都缓存在FBO,性能获得提升。
