using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace Simulator
{
    class Sketch
    {
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern short GPIO_getPinValue(int pin);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void GPIO_setPinValue(int pin, short value);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void Sketch_start();
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void Serial_write([MarshalAs(UnmanagedType.LPStr)]string str, int len);
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]        
        public static extern int Serial_readInto(StringBuilder buffer, int len);

        internal static string Serial_read()
        {
            StringBuilder sb = new StringBuilder(1024);
            int count = Serial_readInto(sb, sb.Capacity);
            return sb.ToString(0, count);
        }
    }
}
