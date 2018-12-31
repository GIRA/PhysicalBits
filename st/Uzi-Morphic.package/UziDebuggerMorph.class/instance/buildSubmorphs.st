building
buildSubmorphs			
	self addMorphBack: (continueButton := self newButton
			label: 'Continue';
			actionSelector: #continue;
			yourself).	
	self addMorphBack: (nextButton := self newButton
			label: 'Next';
			actionSelector: #stepNext;
			yourself).