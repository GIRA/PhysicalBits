building
buildSubmorphs			
	debugger isRunning
		ifTrue: [self addMorphBack: (breakButton := self newButton
			label: 'Break';
			actionSelector: #break;
			yourself)]
		ifFalse: [self addMorphBack: (continueButton := self newButton
			label: 'Continue';
			actionSelector: #continue;
			yourself)].
		
	
	self addMorphBack: (nextButton := self newButton
			label: 'Next';
			actionSelector: #stepNext;
			yourself).