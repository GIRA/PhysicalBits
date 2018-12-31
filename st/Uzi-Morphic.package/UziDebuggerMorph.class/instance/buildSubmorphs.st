building
buildSubmorphs			
	self addMorphBack: (continueButton := self newButton
			label: 'Continue';
			actionSelector: #continue;
			yourself).
	self addMorphBack: (overButton := self newButton
			label: 'Over';
			actionSelector: #stepOver;
			yourself).
	self addMorphBack: (intoButton := self newButton
			label: 'Into';
			actionSelector: #stepInto;
			yourself).
	self addMorphBack: (nextButton := self newButton
			label: 'Next';
			actionSelector: #stepNext;
			yourself).