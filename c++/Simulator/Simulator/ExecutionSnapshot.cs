using System;
using System.Linq;

namespace Simulator
{
    public class ExecutionSnapshot
    {
        public int ms;
        public byte[] pins = new byte[5]; 

        public bool IsDifferentThan(ExecutionSnapshot other)
        {
            return other == null || !pins.SequenceEqual(other.pins);
        }

        public override string ToString()
        {
            return ms + "," + string.Join(",", pins);
        }
    }
}