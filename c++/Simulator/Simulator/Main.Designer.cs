namespace Simulator
{
    partial class Main
    {
        /// <summary>
        /// Variable del diseñador requerida.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Limpiar los recursos que se estén utilizando.
        /// </summary>
        /// <param name="disposing">true si los recursos administrados se deben eliminar; false en caso contrario, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Código generado por el Diseñador de Windows Forms

        /// <summary>
        /// Método necesario para admitir el Diseñador. No se puede modificar
        /// el contenido del método con el editor de código.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.stepTimer = new System.Windows.Forms.Timer(this.components);
            this.pinsTable = new System.Windows.Forms.TableLayoutPanel();
            this.connectBtn = new System.Windows.Forms.Button();
            this.startButton = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.d0 = new System.Windows.Forms.CheckBox();
            this.d1 = new System.Windows.Forms.CheckBox();
            this.d3 = new System.Windows.Forms.CheckBox();
            this.d2 = new System.Windows.Forms.CheckBox();
            this.d7 = new System.Windows.Forms.CheckBox();
            this.d6 = new System.Windows.Forms.CheckBox();
            this.d5 = new System.Windows.Forms.CheckBox();
            this.d4 = new System.Windows.Forms.CheckBox();
            this.d13 = new System.Windows.Forms.CheckBox();
            this.d12 = new System.Windows.Forms.CheckBox();
            this.d11 = new System.Windows.Forms.CheckBox();
            this.d10 = new System.Windows.Forms.CheckBox();
            this.d9 = new System.Windows.Forms.CheckBox();
            this.d8 = new System.Windows.Forms.CheckBox();
            this.a5 = new System.Windows.Forms.CheckBox();
            this.a4 = new System.Windows.Forms.CheckBox();
            this.a3 = new System.Windows.Forms.CheckBox();
            this.a2 = new System.Windows.Forms.CheckBox();
            this.a1 = new System.Windows.Forms.CheckBox();
            this.a0 = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // stepTimer
            // 
            this.stepTimer.Interval = 1;
            this.stepTimer.Tick += new System.EventHandler(this.stepTimer_Tick);
            // 
            // pinsTable
            // 
            this.pinsTable.AutoScroll = true;
            this.pinsTable.BackColor = System.Drawing.SystemColors.ControlDark;
            this.pinsTable.ColumnCount = 1;
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.Location = new System.Drawing.Point(0, 120);
            this.pinsTable.Margin = new System.Windows.Forms.Padding(0);
            this.pinsTable.Name = "pinsTable";
            this.pinsTable.RowCount = 4;
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.Size = new System.Drawing.Size(821, 251);
            this.pinsTable.TabIndex = 6;
            // 
            // connectBtn
            // 
            this.connectBtn.Location = new System.Drawing.Point(12, 12);
            this.connectBtn.Name = "connectBtn";
            this.connectBtn.Size = new System.Drawing.Size(123, 33);
            this.connectBtn.TabIndex = 7;
            this.connectBtn.Text = "Connect";
            this.connectBtn.UseVisualStyleBackColor = true;
            this.connectBtn.Click += new System.EventHandler(this.connectBtn_Click);
            // 
            // startButton
            // 
            this.startButton.Location = new System.Drawing.Point(141, 12);
            this.startButton.Name = "startButton";
            this.startButton.Size = new System.Drawing.Size(123, 33);
            this.startButton.TabIndex = 8;
            this.startButton.Text = "Start";
            this.startButton.UseVisualStyleBackColor = true;
            this.startButton.Click += new System.EventHandler(this.startButton_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 61);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(61, 13);
            this.label1.TabIndex = 10;
            this.label1.Text = "Digital pins:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 88);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(65, 13);
            this.label2.TabIndex = 25;
            this.label2.Text = "Analog pins:";
            // 
            // d0
            // 
            this.d0.AutoSize = true;
            this.d0.Location = new System.Drawing.Point(79, 60);
            this.d0.Name = "d0";
            this.d0.Size = new System.Drawing.Size(32, 17);
            this.d0.TabIndex = 26;
            this.d0.Text = "0";
            this.d0.UseVisualStyleBackColor = true;
            this.d0.CheckedChanged += new System.EventHandler(this.d0_CheckedChanged);
            // 
            // d1
            // 
            this.d1.AutoSize = true;
            this.d1.Location = new System.Drawing.Point(117, 60);
            this.d1.Name = "d1";
            this.d1.Size = new System.Drawing.Size(32, 17);
            this.d1.TabIndex = 27;
            this.d1.Text = "1";
            this.d1.UseVisualStyleBackColor = true;
            this.d1.CheckedChanged += new System.EventHandler(this.d1_CheckedChanged);
            // 
            // d3
            // 
            this.d3.AutoSize = true;
            this.d3.Location = new System.Drawing.Point(193, 60);
            this.d3.Name = "d3";
            this.d3.Size = new System.Drawing.Size(32, 17);
            this.d3.TabIndex = 29;
            this.d3.Text = "3";
            this.d3.UseVisualStyleBackColor = true;
            this.d3.CheckedChanged += new System.EventHandler(this.d3_CheckedChanged);
            // 
            // d2
            // 
            this.d2.AutoSize = true;
            this.d2.Location = new System.Drawing.Point(155, 60);
            this.d2.Name = "d2";
            this.d2.Size = new System.Drawing.Size(32, 17);
            this.d2.TabIndex = 28;
            this.d2.Text = "2";
            this.d2.UseVisualStyleBackColor = true;
            this.d2.CheckedChanged += new System.EventHandler(this.d2_CheckedChanged);
            // 
            // d7
            // 
            this.d7.AutoSize = true;
            this.d7.Location = new System.Drawing.Point(345, 59);
            this.d7.Name = "d7";
            this.d7.Size = new System.Drawing.Size(32, 17);
            this.d7.TabIndex = 33;
            this.d7.Text = "7";
            this.d7.UseVisualStyleBackColor = true;
            this.d7.CheckedChanged += new System.EventHandler(this.d7_CheckedChanged);
            // 
            // d6
            // 
            this.d6.AutoSize = true;
            this.d6.Location = new System.Drawing.Point(307, 59);
            this.d6.Name = "d6";
            this.d6.Size = new System.Drawing.Size(32, 17);
            this.d6.TabIndex = 32;
            this.d6.Text = "6";
            this.d6.UseVisualStyleBackColor = true;
            this.d6.CheckedChanged += new System.EventHandler(this.d6_CheckedChanged);
            // 
            // d5
            // 
            this.d5.AutoSize = true;
            this.d5.Location = new System.Drawing.Point(269, 59);
            this.d5.Name = "d5";
            this.d5.Size = new System.Drawing.Size(32, 17);
            this.d5.TabIndex = 31;
            this.d5.Text = "5";
            this.d5.UseVisualStyleBackColor = true;
            this.d5.CheckedChanged += new System.EventHandler(this.d5_CheckedChanged);
            // 
            // d4
            // 
            this.d4.AutoSize = true;
            this.d4.Location = new System.Drawing.Point(231, 59);
            this.d4.Name = "d4";
            this.d4.Size = new System.Drawing.Size(32, 17);
            this.d4.TabIndex = 30;
            this.d4.Text = "4";
            this.d4.UseVisualStyleBackColor = true;
            this.d4.CheckedChanged += new System.EventHandler(this.d4_CheckedChanged);
            // 
            // d13
            // 
            this.d13.AutoSize = true;
            this.d13.Location = new System.Drawing.Point(573, 59);
            this.d13.Name = "d13";
            this.d13.Size = new System.Drawing.Size(38, 17);
            this.d13.TabIndex = 39;
            this.d13.Text = "13";
            this.d13.UseVisualStyleBackColor = true;
            this.d13.CheckedChanged += new System.EventHandler(this.d13_CheckedChanged);
            // 
            // d12
            // 
            this.d12.AutoSize = true;
            this.d12.Location = new System.Drawing.Point(535, 59);
            this.d12.Name = "d12";
            this.d12.Size = new System.Drawing.Size(38, 17);
            this.d12.TabIndex = 38;
            this.d12.Text = "12";
            this.d12.UseVisualStyleBackColor = true;
            this.d12.CheckedChanged += new System.EventHandler(this.d12_CheckedChanged);
            // 
            // d11
            // 
            this.d11.AutoSize = true;
            this.d11.Location = new System.Drawing.Point(497, 60);
            this.d11.Name = "d11";
            this.d11.Size = new System.Drawing.Size(38, 17);
            this.d11.TabIndex = 37;
            this.d11.Text = "11";
            this.d11.UseVisualStyleBackColor = true;
            this.d11.CheckedChanged += new System.EventHandler(this.d11_CheckedChanged);
            // 
            // d10
            // 
            this.d10.AutoSize = true;
            this.d10.Location = new System.Drawing.Point(459, 60);
            this.d10.Name = "d10";
            this.d10.Size = new System.Drawing.Size(38, 17);
            this.d10.TabIndex = 36;
            this.d10.Text = "10";
            this.d10.UseVisualStyleBackColor = true;
            this.d10.CheckedChanged += new System.EventHandler(this.d10_CheckedChanged);
            // 
            // d9
            // 
            this.d9.AutoSize = true;
            this.d9.Location = new System.Drawing.Point(421, 60);
            this.d9.Name = "d9";
            this.d9.Size = new System.Drawing.Size(32, 17);
            this.d9.TabIndex = 35;
            this.d9.Text = "9";
            this.d9.UseVisualStyleBackColor = true;
            this.d9.CheckedChanged += new System.EventHandler(this.d9_CheckedChanged);
            // 
            // d8
            // 
            this.d8.AutoSize = true;
            this.d8.Location = new System.Drawing.Point(383, 60);
            this.d8.Name = "d8";
            this.d8.Size = new System.Drawing.Size(32, 17);
            this.d8.TabIndex = 34;
            this.d8.Text = "8";
            this.d8.UseVisualStyleBackColor = true;
            this.d8.CheckedChanged += new System.EventHandler(this.d8_CheckedChanged);
            // 
            // a5
            // 
            this.a5.AutoSize = true;
            this.a5.Location = new System.Drawing.Point(269, 86);
            this.a5.Name = "a5";
            this.a5.Size = new System.Drawing.Size(32, 17);
            this.a5.TabIndex = 45;
            this.a5.Text = "5";
            this.a5.UseVisualStyleBackColor = true;
            this.a5.CheckedChanged += new System.EventHandler(this.a5_CheckedChanged);
            // 
            // a4
            // 
            this.a4.AutoSize = true;
            this.a4.Location = new System.Drawing.Point(231, 86);
            this.a4.Name = "a4";
            this.a4.Size = new System.Drawing.Size(32, 17);
            this.a4.TabIndex = 44;
            this.a4.Text = "4";
            this.a4.UseVisualStyleBackColor = true;
            this.a4.CheckedChanged += new System.EventHandler(this.a4_CheckedChanged);
            // 
            // a3
            // 
            this.a3.AutoSize = true;
            this.a3.Location = new System.Drawing.Point(193, 87);
            this.a3.Name = "a3";
            this.a3.Size = new System.Drawing.Size(32, 17);
            this.a3.TabIndex = 43;
            this.a3.Text = "3";
            this.a3.UseVisualStyleBackColor = true;
            this.a3.CheckedChanged += new System.EventHandler(this.a3_CheckedChanged);
            // 
            // a2
            // 
            this.a2.AutoSize = true;
            this.a2.Location = new System.Drawing.Point(155, 87);
            this.a2.Name = "a2";
            this.a2.Size = new System.Drawing.Size(32, 17);
            this.a2.TabIndex = 42;
            this.a2.Text = "2";
            this.a2.UseVisualStyleBackColor = true;
            this.a2.CheckedChanged += new System.EventHandler(this.a2_CheckedChanged);
            // 
            // a1
            // 
            this.a1.AutoSize = true;
            this.a1.Location = new System.Drawing.Point(117, 87);
            this.a1.Name = "a1";
            this.a1.Size = new System.Drawing.Size(32, 17);
            this.a1.TabIndex = 41;
            this.a1.Text = "1";
            this.a1.UseVisualStyleBackColor = true;
            this.a1.CheckedChanged += new System.EventHandler(this.a1_CheckedChanged);
            // 
            // a0
            // 
            this.a0.AutoSize = true;
            this.a0.Location = new System.Drawing.Point(79, 87);
            this.a0.Name = "a0";
            this.a0.Size = new System.Drawing.Size(32, 17);
            this.a0.TabIndex = 40;
            this.a0.Text = "0";
            this.a0.UseVisualStyleBackColor = true;
            this.a0.CheckedChanged += new System.EventHandler(this.a0_CheckedChanged);
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(830, 380);
            this.Controls.Add(this.a5);
            this.Controls.Add(this.a4);
            this.Controls.Add(this.a3);
            this.Controls.Add(this.a2);
            this.Controls.Add(this.a1);
            this.Controls.Add(this.a0);
            this.Controls.Add(this.d13);
            this.Controls.Add(this.d12);
            this.Controls.Add(this.d11);
            this.Controls.Add(this.d10);
            this.Controls.Add(this.d9);
            this.Controls.Add(this.d8);
            this.Controls.Add(this.d7);
            this.Controls.Add(this.d6);
            this.Controls.Add(this.d5);
            this.Controls.Add(this.d4);
            this.Controls.Add(this.d3);
            this.Controls.Add(this.d2);
            this.Controls.Add(this.d1);
            this.Controls.Add(this.d0);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.startButton);
            this.Controls.Add(this.connectBtn);
            this.Controls.Add(this.pinsTable);
            this.Name = "Main";
            this.Text = "Arduino Simulator";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Main_FormClosing);
            this.Load += new System.EventHandler(this.Main_Load);
            this.Resize += new System.EventHandler(this.Main_Resize);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Timer stepTimer;
        private System.Windows.Forms.TableLayoutPanel pinsTable;
        private System.Windows.Forms.Button connectBtn;
        private System.Windows.Forms.Button startButton;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.CheckBox d0;
        private System.Windows.Forms.CheckBox d1;
        private System.Windows.Forms.CheckBox d3;
        private System.Windows.Forms.CheckBox d2;
        private System.Windows.Forms.CheckBox d7;
        private System.Windows.Forms.CheckBox d6;
        private System.Windows.Forms.CheckBox d5;
        private System.Windows.Forms.CheckBox d4;
        private System.Windows.Forms.CheckBox d13;
        private System.Windows.Forms.CheckBox d12;
        private System.Windows.Forms.CheckBox d11;
        private System.Windows.Forms.CheckBox d10;
        private System.Windows.Forms.CheckBox d9;
        private System.Windows.Forms.CheckBox d8;
        private System.Windows.Forms.CheckBox a5;
        private System.Windows.Forms.CheckBox a4;
        private System.Windows.Forms.CheckBox a3;
        private System.Windows.Forms.CheckBox a2;
        private System.Windows.Forms.CheckBox a1;
        private System.Windows.Forms.CheckBox a0;


    }
}

