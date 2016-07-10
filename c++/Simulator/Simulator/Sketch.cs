using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace ArduinoSim
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
    }
}
