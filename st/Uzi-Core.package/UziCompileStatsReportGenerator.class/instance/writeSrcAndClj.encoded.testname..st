as yet unclassified
writeSrcAndClj: uziString encoded: bytes testname: name
"
(deftest {1}  
  (let [expected {2}
        program {3}
        actual (en/encode program)]
    (is (= expected actual))))
"
	| clj src template |
	template := '(deftest {1}-uzi-string  
  (let [expected (en/encode {2})
        program (cc/compile-uzi-string "{3}")
        actual (en/encode program)]
    (is (= expected actual))))' copyReplaceAll: {Character cr} with: String crlf.

	clj := String streamContents: [:stream || index program |
		index := 0.
		program := Uzi compileString: uziString.
		program cljString linesDo: [:line |
			index = 0 ifFalse: [stream crlf.
				8 timesRepeat: [stream space]].
			stream nextPutAll: line.
			index := index + 1]].
	
	src := String streamContents: [:stream || index |
		index := 0.
		uziString linesDo: [:line |
			index = 0 ifFalse: [stream crlf.
				8 timesRepeat: [stream space]].
			stream nextPutAll: (line copyReplaceAll: '"' with: '\"').
			index := index + 1]].
	
	out nextPutAll: (template format: { name capitalized . clj . src}).
	out  crlf; crlf.
		