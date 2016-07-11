using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Collections.Concurrent;
using System.Threading;
using System.Diagnostics;

namespace Simulator
{
    public class Sketch
    {
        private const string DLL_NAME = "Sketch";
        
        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
        private static extern short GPIO_getPinValue(int pin);

        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
        private static extern void GPIO_setPinValue(int pin, short value);

        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
        private static extern void Sketch_setup();

        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
        private static extern void Sketch_loop();

        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
        private static extern void Serial_write([MarshalAs(UnmanagedType.LPStr)]string str, int len);
        
        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]        
        private static extern int Serial_readInto(StringBuilder buffer, int len);

        private static Sketch current = new Sketch();
        public static Sketch Current { get { return current; } }

        private ConcurrentQueue<Tuple<DateTime, string>> serial;
        private Thread thread;
        private bool running = false;
        private bool paused = false;

        private Sketch() {}

        public bool Running { get { return running; } }
        public bool Paused { get { return paused; } }
        public bool Stopped { get { return !running && !paused; } }

        public short GetPinValue(int pin)
        {
            return GPIO_getPinValue(pin);
        }

        public void SetPinValue(int pin, short value)
        {
            GPIO_setPinValue(pin, value);
        }

        public void Start()
        {
            if (running) return;
            running = true;
            thread = new Thread(Main);
            thread.Start();
        }

        public void Pause()
        {
            paused = true;
            Stop();
        }

        public void Stop()
        {
            running = false;
        }
        
        public void WriteSerial(string str)
        {
            Serial_write(str, str.Length);
        }
        
        public Tuple<DateTime,string> ReadSerial()
        {
            if (serial == null) return null;
            Tuple<DateTime, string> result;
            if (serial.TryDequeue(out result))
            {
                return result;
            }
            return null;
        }

        private void InitSerial()
        {
            serial = new ConcurrentQueue<Tuple<DateTime, string>>();
        }

        private void EnqueueSerial()
        {
            StringBuilder sb = new StringBuilder(1024);
            int count = Serial_readInto(sb, sb.Capacity);
            string read = sb.ToString(0, count);
            if (!string.IsNullOrWhiteSpace(read))
            {
                serial.Enqueue(new Tuple<DateTime, string>(DateTime.Now, read));
            }
        }

        private void Main()
        {
            if (paused) { paused = false; }
            else
            {
                InitSerial();
                Sketch_setup();
            }
            while (running)
            {
                EnqueueSerial();
                Sketch_loop();
            }
        }
    }
}
