accessing
names
	^ names ifNil: [names := Dictionary new
		at: 2 put: 'D2'; at: 3 put: 'D3'; at: 4 put: 'D4'; at: 5 put: 'D5'; at: 6 put: 'D6';
		at: 7 put: 'D7'; at: 8 put: 'D8'; at: 9 put: 'D9'; at: 10 put: 'D10'; at: 11 put: 'D11';
		at: 12 put: 'D12'; at: 13 put: 'D13'; at: 14 put: 'A0'; at: 15 put: 'A1'; at: 16 put: 'A2';
		at: 17 put: 'A3'; at: 18 put: 'A4'; at: 19 put: 'A5';
		yourself]