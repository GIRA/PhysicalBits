drawing
drawOn: aCanvas
	aCanvas 
		clipBy: self bounds
		during: [:canvas || offset step i |
			offset := 5@5.
			step := 0@25.
			i := 0.
			canvas
				fillRectangle: self bounds
				color: Color white;
				frameRectangle: self bounds 
				width: 2
				color: (protocol isConnected ifTrue: [Color green] ifFalse: [Color red]);
				drawString: ('Connected: {1}' format: { protocol isConnected })
				at: self topLeft + (i * step + offset).
			i := i + 1.

			protocol pinsReporting ifNotEmpty: [:pins |
				canvas 
					drawString: 'Pins:'
					at: self topLeft + (i * step + offset).
				i := i + 1.
				pins asSortedCollection withIndexDo: [:pin :index |
					canvas 
						drawString: ('  {1} -> {2}' format: { pin . protocol getPinValue: pin})
						at: self topLeft + (i * step + offset).
					i := i + 1]].
			
			protocol variables ifNotEmpty: [:vars |
				canvas 
					drawString: 'Variables:'
					at: self topLeft + (i * step + offset).
				i := i + 1.
				vars asSortedCollection withIndexDo: [:var :index |
					canvas 
						drawString: ('  #{1} -> {2}' format: { var . protocol getGlobal: var})
						at: self topLeft + (i * step + offset).
					i := i + 1]]]