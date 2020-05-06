as yet unclassified
write: assertions named: testName on: out 
	out nextPutAll: '(deftest '.
	out nextPutAll: testName capitalized; crlf.
	assertions
		do: [:assertion || src valid |
			src := assertion key.
			valid := assertion value.
			out nextPutAll: ('  (is ({1}? {2}))' format: { 
				valid ifTrue: ['valid'] ifFalse: ['invalid'].
				src isString
					ifTrue: ['"' , (self formatSrc: src) , '"']
					ifFalse: [src cljString]
			})]
		separatedBy: [out crlf].
	out nextPutAll: ')'; crlf; crlf