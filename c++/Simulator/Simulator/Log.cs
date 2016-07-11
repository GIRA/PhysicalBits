using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

using System.Collections.Concurrent;

namespace SSDC.UI
{
    public partial class Log : UserControl
    {
        public Log()
        {
            InitializeComponent();
        }

        private ConcurrentQueue<string> lines = new ConcurrentQueue<string>();

        public void AppendLine()
        {
            AppendLine("");
        }

        public void AppendLine(object obj)
        {
            AppendLine("{0}", obj);
        }

        public void AppendLine(string format, params object[] args)
        {
            // string.Format prints empty string for null args, 
            // but I want null objects to be printed as "null"
            object[] notNullArgs = args.Select((each) => each == null ? "null" : each).ToArray();
            string msg = notNullArgs.Length > 0 ? string.Format(format, notNullArgs) : format;
            lines.Enqueue(msg);
        }

        private void updateTimer_Tick(object sender, EventArgs e)
        {
            int start = Environment.TickCount;
            do
            {
                string line;
                if (lines.TryDequeue(out line))
                {
                    logTextBox.AppendText(line);
                    logTextBox.AppendText(Environment.NewLine);
                }
            }
            while (lines.Count > 0 && Environment.TickCount - start < updateTimer.Interval);
        }

        private void Log_FontChanged(object sender, EventArgs e)
        {
            logTextBox.Font = Font;
        }

        private void Log_Load(object sender, EventArgs e)
        {
            logTextBox.Font = Font;
        }
    }
}
