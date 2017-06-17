grammar-scripts
statementsList
	^ ws, statement star , ws map: [:ign1 :stmts :ign2 | stmts]