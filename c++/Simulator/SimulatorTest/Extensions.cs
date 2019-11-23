using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SimulatorTest
{
    public static class Extensions
    {
        public static double Median(this IEnumerable<double> data)
        {
            var sorted = data.ToList();
            sorted.Sort();
            if (sorted.Count % 2 == 0)
            {
                double a = sorted[(sorted.Count / 2) - 1];
                double b = sorted[(sorted.Count / 2)];
                return (a + b) / 2.0;
            }
            else
            {
                return sorted[sorted.Count / 2];
            }
        }

        public static double Median<T>(this IEnumerable<T> data, Func<T, double> func)
        {
            return data.Select(func).Median();
        }
    }
}
