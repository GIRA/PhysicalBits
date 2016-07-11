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
        private static extern short GPIO_getPinValue(int pin);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GPIO_setPinValue(int pin, short value);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void Sketch_start();
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void Serial_write([MarshalAs(UnmanagedType.LPStr)]string str, int len);
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]        
        private static extern int Serial_readInto(StringBuilder buffer, int len);

        public static short GetPinValue(int pin)
        {
            return GPIO_getPinValue(pin);
        }

        public static void SetPinValue(int pin, short value)
        {
            GPIO_setPinValue(pin, value);
        }

        public static void Start()
        {
            Sketch_start();
        }

        public static void WriteSerial(string str)
        {
            Serial_write(str, str.Length);
        }

        public static string ReadSerial()
        {
            StringBuilder sb = new StringBuilder(1024);
            int count = Serial_readInto(sb, sb.Capacity);
            return sb.ToString(0, count);
        }
    }
}
