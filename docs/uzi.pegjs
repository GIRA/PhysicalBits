start = program

comments = ('"' (!'"'.)+ '"')+
ws = ([ \r\n\t] / comments)*

digits = [0-9]+
integer = '-'? digits
float = 'Infinity' / '-Infinity' / 'NaN' / (integer '.' digits)
number = ws (float / integer) ws
constant = ws [DA] integer ws

letter = [a-zA-Z]
word = [0-9a-zA-Z]
identifier = (letter / '_') (word / '_')* ('.' (letter / '_') (word / '_')*)*

literal = constant / number
scriptReference = ws identifier ws
call = ws scriptReference argsList ws
namedArg = (identifier ':')? expression
argsList = ws '(' ws (namedArg (ws ',' ws namedArg)*)? ws ')' ws
variable = ws identifier ws
subexpression = ws '(' ws expression ws ')' ws
expressionNotBinary = literal / call / variable / subexpression
binarySelector = (![a-zA-Z0-9\[\](){}".':#,;_].)+
binary = ws expressionNotBinary (ws binarySelector expressionNotBinary)+ ws
expression = ws ('!' ws)? (binary / expressionNotBinary)

paramsList = ws '(' ws (variable (ws ',' ws variable)*)? ws ')' ws

statementsList = ws statement* ws
assignment = ws variable ws '=' ws expression ws ';' ws
return = ws 'return' ws expression? ws ';' ws
conditional = ws 'if' ws expression ws block ws ('else' ws block)? ws
scriptList = scriptReference (ws ',' ws scriptReference)*
startTask = ws 'start' ws scriptList ';' ws
stopTask = ws 'stop' ws scriptList ';' ws
pauseTask = ws 'pause' ws scriptList ';' ws
resumeTask = ws 'resume' ws scriptList ';' ws
while = ws 'while' ws expression ws (block / ';') ws
doWhile = ws 'do' ws block ws 'while' ws expression ws ';' ws
until = ws 'until' ws expression ws (block / ';') ws
doUntil = ws 'do' ws block ws 'until' ws expression ws ';' ws
repeat = ws 'repeat' ws expression ws block ws
for = ws 'for' ws variable ws '=' ws expression ws 'to' ws expression ws 
    ('by' ws expression)? ws block ws
forever = ws 'forever' ws block ws
expressionStatement = expression ws ';'
statement = variableDeclaration 
            / assignment / return / conditional
            / startTask / stopTask 
            / pauseTask / resumeTask
            / while / doWhile
            / until / doUntil
            / repeat / for / forever
            / expressionStatement

importPath = "'" (!"'".)* "'"
import = ws 'import' ws identifier ws 'from' ws importPath ws ';' ws

variableDeclaration = ws 'var' ws variable ws ('=' expression)? ws ';' ws
block = ws '{' variableDeclaration* statementsList '}' ws

tickingRate = number ws '/' ws [smhd]
taskState = ws ('running' / 'stopped' / 'once') ws
task = ws 'task' ws identifier paramsList taskState tickingRate? block ws
function = ws 'func' ws identifier paramsList block ws
procedure =  ws 'proc' ws identifier paramsList block ws
script = task / function / procedure

program = ws import* ws variableDeclaration* ws script* ws