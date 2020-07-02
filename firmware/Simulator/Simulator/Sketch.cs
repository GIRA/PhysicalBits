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
    public class RuntimeStats
    {
        public uint UsedMemory { get; set; }
        public uint CoroutineResizeCounter { get; set;  }
    }

    public class Sketch
    {
        private static Sketch current = new Sketch();
        public static Sketch Current { get { return current; } }

        public event Action<Tuple<DateTime, byte[]>> SerialReceived = (tuple) => { };

        private ConcurrentQueue<Tuple<DateTime, byte[]>> serial;
        private object serial_lock = new object();
        private Thread thread;
        private bool running = false;
        private bool paused = false;
        private List<RuntimeStats> stats;

        private Sketch() {}

        public bool Running { get { return running; } }
        public bool Paused { get { return paused; } }
        public bool Stopped { get { return !running && !paused; } }
        public IEnumerable<RuntimeStats> Stats { get { return stats; } }

        public short GetPinValue(int pin)
        {
            return DLL.GPIO_getPinValue(pin);
        }

        public void SetPinValue(int pin, short value)
        {
            DLL.GPIO_setPinValue(pin, value);
        }

        public void Start()
        {
            if (running) return;
            running = true;
            thread = new Thread(Main);
            thread.IsBackground = true;
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

        public byte ReadEEPROM(int address)
        {
            if (address >= 0 && address < DLL.EEPROM_size())
            {
                return DLL.EEPROM_read(address);
            }
            else
            {
                // Reading outside the memory
                return 0;
            }
        }

        public byte[] ReadEEPROM()
        {
            byte[] eeprom = new byte[DLL.EEPROM_size()];
            for (int i = 0; i < eeprom.Length; i++)
            {
                eeprom[i] = ReadEEPROM(i);
            }
            return eeprom;
        }

        public void WriteEEPROM(int address, byte value)
        {
            if (address >= 0 && address < DLL.EEPROM_size())
            {
                DLL.EEPROM_write(address, value);
            }
        }

        public void WriteEEPROM(byte[] values)
        {
            for (int i = 0; i < values.Length; i++)
            {
                WriteEEPROM(i, values[i]);
            }
        }

        public void WriteSerial(byte[] bytes)
        {
            lock (serial_lock)
            {
                DLL.Serial_write(bytes, bytes.Length);
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
                int count = DLL.Serial_readInto(buffer, buffer.Length);
                byte[] read = buffer.Take(count).ToArray();
                if (read.Length > 0)
                {
                    Tuple<DateTime, byte[]> tuple = new Tuple<DateTime, byte[]>(DateTime.Now, read);
                    serial.Enqueue(tuple);
                    SerialReceived(tuple);
                }
            }
        }

        private void Main()
        {
            if (paused) { paused = false; }
            else
            {
                Setup();
            }
            while (running)
            {
                Loop();
            }
        }

        public void Setup()
        {
            InitSerial();
            DLL.Sketch_setup();
        }

        public void Loop()
        {
            DLL.Sketch_loop();
            EnqueueSerial();
            if (stats != null)
            {
                stats.Add(new RuntimeStats()
                {
                    UsedMemory = DLL.Stats_usedMemory(),
                    CoroutineResizeCounter = DLL.Stats_coroutineResizeCounter()
                });
            }
        }

        public int GetMillis()
        {
            return DLL.Sketch_getMillis();
        }

        public void SetMillis(int millis)
        {
            DLL.Sketch_setMillis(millis);
        }

        public void RegisterStats(bool enabled)
        {
            stats = enabled ? new List<RuntimeStats>() : null;
        }
        
        private static class DLL
        {
            private const string DLL_NAME = "Sketch";

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern short GPIO_getPinValue(int pin);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void GPIO_setPinValue(int pin, short value);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void Sketch_setup();

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void Sketch_loop();

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void Serial_write(byte[] str, int len);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern int Serial_readInto(byte[] buffer, int len);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern int Sketch_getMillis();

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void Sketch_setMillis(int millis);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern byte EEPROM_read(int address);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern void EEPROM_write(int address, byte value);

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern int EEPROM_size();

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern uint Stats_usedMemory();

            [DllImport(DLL_NAME, CallingConvention = CallingConvention.Cdecl)]
            public static extern uint Stats_coroutineResizeCounter();
        }
    }
}