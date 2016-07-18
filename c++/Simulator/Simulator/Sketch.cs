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
        private static extern void Serial_write(byte[] str, int len);
        
        [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]        
        private static extern int Serial_readInto(byte[] buffer, int len);

        private static Sketch current = new Sketch();
        public static Sketch Current { get { return current; } }

        private ConcurrentQueue<Tuple<DateTime, byte[]>> serial;
        private object serial_lock = new object();
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
                
        public void WriteSerial(byte[] bytes)
        {
            lock (serial_lock)
            {
                Serial_write(bytes, bytes.Length);
            }
        }
        
        public Tuple<DateTime, byte[]> ReadSerial()
        {
            if (serial == null) return null;
            Tuple<DateTime, byte[]> result;
            if (serial.TryDequeue(out result))
            {
                return result;
            }
            return null;
        }

        private void InitSerial()
        {
            serial = new ConcurrentQueue<Tuple<DateTime, byte[]>>();
        }

        private void EnqueueSerial()
        {
            lock (serial_lock)
            {
                byte[] buffer = new byte[1024];
                int count = Serial_readInto(buffer, buffer.Length);
                byte[] read = buffer.Take(count).ToArray();
                if (read.Length > 0)
                {
                    serial.Enqueue(new Tuple<DateTime, byte[]>(DateTime.Now, read));
                }
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
