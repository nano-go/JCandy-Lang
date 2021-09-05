if exists("b:current_syntax")
	finish
endif

let b:current_syntax="candy"

syn case match

syn cluster cdNotTop contains=cdException,cdComment,cdCommentTodo,cdConditional,cdException,cdStatements,cdRepeat,cdDebug,cdDeclaration,cdString,cdBlock,cdIdentifier

syn keyword cdDeclaration        var static pri pub reader writer

hi def link cdDeclaration        Keyword

syn keyword cdStatements         break continue raise return import
syn keyword cdConditional        if else
syn keyword cdRepeat             for in while
syn keyword cdException          try intercept as finally

hi def link cdStatements         Statement
hi def link cdConditional        Conditional
hi def link cdRepeat             Repeat
hi def link cdException          Exception

syn keyword cdBuiltin            print println max min range curTime importMudule setAttr getAttr str bool methods select selectByFilter cmdArgs loadLibrary readLine sleep array tuple repeat exit
syn keyword cdConstants          true false null

hi def link cdBuiltin            Identifier
hi def link cdConstants          Constant

syn keyword cdSpeciIdentifier    this super
hi def link cdSpeciIdentifier    Identifier

syn keyword cdOperator           and or is
hi def link cdOperator           Operator

syn keyword cdDebug              assert
hi def link cdDebug              Debug

" Comments
syn keyword cdCommentTodo        TODO FIXME XXX BUG NOTE contained
syn cluster cdCommentGroup       contains=cdCommentTodo

syn region cdComment             start="/\*" end="\*/" contains=@cdCommentGroup,@Spell fold 
syn region cdComment             start=+//+ end=/$/ contains=@cdCommentGrouap,@Spell

hi def link cdComment            Comment
hi def link cdCommentTodo        Todo

" Identifier;
syn match cdIdentifier           /[a-zA-Z_][a-zA-Z0-9_]*/ skipwhite skipnl contained
syn match cdAtIdentifier         /@/ nextgroup=cdIdentifier

hi def link cdIdentifier         Identifier
hi def link cdAtIdentifier       Identifier

" Function;
syn match cdDeclaration          /\<fun\>/ nextgroup=cdIdentifier skipwhite skipnl

" Class;
syn match cdDeclaration          /\<class\>/ nextgroup=cdIdentifier skipwhite skipnl

" Lambda
syn match cdExprKey              /lambda/
syn match cdLambda               /->/

hi def link cdLambda             Operator
hi def link cdExprKey            Keyword

" String
syn match cdUnicodeEscape        "\\[0-7]\{3}" contained
syn match cdUnicodeEscape        "\\u[0-9a-fA-F]\{4}" contained
syn match cdUnicodeError         "\\[0-7]\{1,2}[^0-7"]" contained
syn match cdUnicodeError         "\\u[0-9a-fA-F]\{1,3}[^0-9a-fA-F"]" contained

syn match cdSingleCharEscape     "\\[trnfb'"{}\$]" contained

syn region cdInterpolation       matchgroup=cdInterpolationDelimiter start=+${+ end=+}+ contains=ALLBUT,@cdNotTop contained
syn region cdStrInInterpolation  start=+\\"+ end=+\\"+ display contains=cdInterpolation,cdErrorInterpolation,cdUnicodeEscape,cdUnicodeError,cdStringSpecial contained
syn region cdString              start=+"+ skip=+\\"+ end=+"+ display contains=cdInterpolation,cdErrorInterpolation,cdUnicodeEscape,cdUnicodeError,cdSingleCharEscape

hi def link cdSingleCharEscape   cdStringSpecial
hi def link cdUnicodeEscape      cdStringSpecial

hi def link cdStringSpecial      Special
hi def link cdString             String
hi def link cdStrInInterpolation String
hi def link cdUnicodeError       Error

hi def link cdInterpolationDelimiter Delimiter

" Region
syn region cdBlock               start="{" end="}" transparent fold
syn region cdArray               start="\[" end="]" transparent fold
syn region cdParen               start="(" end=")" transparent

" Candy Integers
syn match cdDecimalInt           "\<\d\+\>"
syn match cdBinaryInt            "\<0[bB][01]\+\>"
syn match cdOctInt               "\<0[oO]\=\o\+\>"
syn match cdHexInt               "\<0[xX]\x\+\>"

syn match cdBinaryIntError       "\<0[bB][01]\+[2345678abcdefABCDEF]\x*\>"
syn match cdDecimalIntError      "\<[123456789]\d*[abcdefABCDEF]\x*\>"
syn match cdOctIntError          "\<0[oO]\=\o\+[89abcdefABCDEF]\x*\>"

hi def link cdDecimalInt         Integer
hi def link cdOctInt             Integer
hi def link cdHexInt             Integer
hi def link cdBinaryInt          Integer
hi def link Integer              Number

syn match cdFloat                "\<\d+\.\d+\>"

hi def link cdFloat              Float
hi def link Float                Number

hi def link cdOctIntError        Error
hi def link cdDecimalIntError    Error
hi def link cdBinaryIntError     Error

syn sync minlines=500

