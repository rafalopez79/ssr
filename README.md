#SSR SteganoSaurus Rex

Basic Steganography. 
- Saves a byte array into a PNG image using the least significative bits of each pixel.
- It requires lossless compression (PNG).
- It needs an image with as many as pixels as the message length plus 12.

##Usage

SSRCoder.encode ecodes the message into an ARGB image.
SSRCoder.decode decodes the message stored into an ARGB image.