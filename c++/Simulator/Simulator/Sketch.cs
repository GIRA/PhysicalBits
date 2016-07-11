using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Collections.Concurrent;
using System.Threading;

namespace Simulator
{
    class Sketch
    {
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern short GPIO_getPinValue(int pin);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GPIO_setPinValue(int pin, short value);

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void Sketch_setup();

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void Sketch_loop();

        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]
        private static extern void Serial_write([MarshalAs(UnmanagedType.LPStr)]string str, int len);
        
        [DllImport("Sketch", CallingConvention = CallingConvention.Cdecl)]        
        private static extern int Serial_readInto(StringBuilder buffer, int len);

        private static ConcurrentQueue<Tuple<DateTime, string>> serial;
        private static Thread thread;
        private static bool running = false;

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
            if (running) return;
            running = true;
            thread = new Thread(Process);
            thread.Start();
        }

        public static void Stop()
        {
            running = false;
        }

        public static bool Running { get { return running; } }
        
        public static void WriteSerial(string str)
        {
            Serial_write(str, str.Length);
        }
        
        public static Tuple<DateTime,string> ReadSerial()
        {
            if (serial == null) return null;
            Tuple<DateTime, string> result;
            if (serial.TryDequeue(out result))
            {
                return result;
            }
            return null;
        }

        private static void InitSerial()
        {
            serial = new ConcurrentQueue<Tuple<DateTime, string>>();
        }

        private static void EnqueueSerial()
        {
            StringBuilder sb = new StringBuilder(1024);
            int count = Serial_readInto(sb, sb.Capacity);
            string read = sb.ToString(0, count);
            if (!string.IsNullOrWhiteSpace(read))
            {
                serial.Enqueue(new Tuple<DateTime, string>(DateTime.Now, read));
            }
        }

        private static void Process()
        {
            InitSerial();
            Sketch_setup();
            while (running)
            {
                EnqueueSerial();
                Sketch_loop();
            }
        }
    }
}
