printing
printOn: aStream
	"Creates the string representation for this cookie"

	aStream nextPutAll: name, '=', value asString.
	expiry ifNotNil:[ 
		aStream nextPutAll:'; expires='.
		aStream nextPutAll: expiry asDate weekday, ', '.
		expiry asDate printOn: aStream format: #(1 2 3 $- 2 2 2 ).
		aStream nextPutAll: ' ', expiry asTime print24,' GMT'.
	].
	path ifNotNil:[
		aStream nextPutAll:'; path=', (WebUtils quote: path).
	].
	domain ifNotNil:[
		aStream nextPutAll:'; domain=', (WebUtils quote: domain).
	].
	version ifNotNil:[
		aStream nextPutAll:'; version=', version asString.
	].
	secure ifTrue:[
		aStream nextPutAll: '; secure'.
	].
	httpOnly ifTrue:[
		aStream nextPutAll: '; HttpOnly'.
	].
