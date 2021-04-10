# UEIT-Java
Java implementation of Universal Television Test Table generator. Requires Java 8 or later.

Usage:

* When launched without any arguments, a full-screen UEIT window opens which tries to maintain 60 frames per second.
* A different target frame rate can be specified as a first command line argument.
* To generate a frame set in a form of C source code for using with Xilinx VDMA IP, specify the following arguments:
	* Target frame rate (will be imprinted to the frames)
	* Total number of frames to be generated
	* Frame width in pixels
	* Frame height in pixels

Example: java -jar UEIT.jar 60 32 1920 1080

This will generate two files: ueit_60_32_1920_1080.h and ueit_60_32_1920_1080.c, containing a 2D const array of uint32_t with ARGB pixel data.
For the given inputs the source file will be 800+ MiB in size, but GCC is still able to compile it into 250+ MiB binary %)
