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
        public static extern int foo();

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern Int16 getPinValue(Int32 pin);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void setPinValue(Int32 pin, Int16 value);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void start();
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        public static extern void serial_write([MarshalAs(UnmanagedType.LPStr)]string str, int len);
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]        
        public static extern int serial_readInto(StringBuilder buffer, int len);

        internal static string serial_read()
        {
            StringBuilder sb = new StringBuilder(1024);
            int count = serial_readInto(sb, sb.Capacity);
            return sb.ToString(0, count);
        }
    }
}
