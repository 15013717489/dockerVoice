# dockerVoice
项目名：dockerVoice(听医声)
后端程序地址(github)：https://github.com/15013717489/dockerVoice
实现框架：springboot+javax.sound+ffmpeg
实现原理：
通过原生openJDK8包下的处理音频包javax.Sound生成一段音频AudioInputStream+AudioSystem二进制流生成对应的声音类型（已实现初步生成纯音，噪音，脉冲，纯音脉冲，高斯噪声，啭音，均匀噪声，未实现掩蔽噪声(需要掩蔽算法)）并生成一个临时.mav文件,再通过ffmpeg进行混音。
遇到问题：
1.（重要）这套架构需要后端生成音频 但是前后端传输文件(一个30分钟的mav音频文件需要10M)无法实现像PC端一样实时调整生成音频。
2.后端生成音频文件需要时间，预计可以采用多线程减少生成的时间countDownLatch或completefuture
3.服务器内存需要存储音频的临时文件 预计可以采用缓存技术处理 如redis , java cache
4.java的库对服务器 数据处理擅长 对生成音频不擅长 已研究jsyn,beads,TarsosDSP等java音频库 此类库只能处理生成音频后录音 没有转二进制的功能 而且录音需要时间 不适合此需求。
