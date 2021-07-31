if exists("b:did_indent")
    finish
endif
let b:did_indent = 1

setlocal nolisp
setlocal autoindent
setlocal indentkeys+=0=},0=),0=]
setlocal indentexpr=CandyIndent(v:lnum)

if exists("*CandyIndent")
    finish
endif

function! CandyIndent(lnum) abort
    let prevlnum = prevnonblank(a:lnum-1)
    " Means top of file.
    if prevlnum == 0
        return 0
    endif

    " Previous line, ignore comment.
    let prevl = substitute(getline(prevlnum), '//.*$', '', '')
    " Current line, ignore comment.
    let thisl = substitute(getline(a:lnum), '//.*$', '', '')
    " Previous indent.
    let previ = indent(prevlnum)

    let ind = previ

    " Open block: '(', '[', '{'
    if prevl =~ '[({\[]\s*$'
        let ind += shiftwidth()
    endif

    " Close block: ')', ']', '}'
    if thisl =~ '^\s*[)}\]]'
        let ind -= shiftwidth()
    endif
    return ind
endfunction
