authentication
passwordAt: username realm: realm put: password
	"Store the password hash for the key."

	| hash |
	hash := self hashUser: username password: password realm: realm.
	self passwordHashAt: (username,':',realm) put: hash