# The script of generating 'Instructions.java' code.

from enum import Enum

INSTRUCTION_NAMES_FIELD_NAME = "INSTRUCTION_NAMES"
CLASS_NAME = "Instructions"

# Java Code Format

# {0} is name, {1} is spaces, {2} is byte value.
FORMAT_INSTRUCTION_FIELD = "public static final byte {0}{1} = (byte){2};"

FORMAT_INSTRUCTION_NUMBER_FIELD = "public static final byte INSTRUCTION_NUMBER = {0};"

# {0} is the number of the instruction.
FORMAT_INSTRUCTION_NAME_ARRAY_FIELD = "public static final String[] " + INSTRUCTION_NAMES_FIELD_NAME + " = new String[{0}];"

# {0} is index, {1} is spaces, {2} is name literal
FORMAT_INSTRUCTION_NAME_FIELD = INSTRUCTION_NAMES_FIELD_NAME + '[{0}]{1} = "{2}";'

CODE_PACKAGE = "package com.nano.candy.interpreter.i2.instruction;\n\n"
CODE_CLASS   = "public class " + CLASS_NAME + " {"

CODE_NAME_GETTER = (
    "\tpublic static String getName(byte opcode) {\n"
    "\t\treturn " + INSTRUCTION_NAMES_FIELD_NAME + "[opcode];\n"
    "\t}"
)

class LineType(Enum):
    COMMENT      = 0
    INSTRUCTION  = 1
    SPACE        = 2

def parseLine(line, byteValue):
    if line.startswith('//'):
        return (LineType.COMMENT, line)
    if len(line) == 0:
        return (LineType.SPACE,)
    names = line.split(' ')
    # (LineType, FieldName, DisName, ByteValue)
    return (LineType.INSTRUCTION, names[0], names[1], byteValue)


# Returns lines and the maximum length of the instruction name.
def parseFile(fpath):
    with open(fpath, 'r') as f:
        content = f.read()
        lines = []
        byteValue = 0
        maxlenOfName = 0
        for line in content.splitlines():
            line = parseLine(line, byteValue)
            lines.append(line)
            if line[0] == LineType.INSTRUCTION:
                byteValue += 1
                maxlenOfName = max(maxlenOfName, len(line[1]))
        return (lines, maxlenOfName)

def genJavaCode(parsedFile):
    lines=parsedFile[0]
    maxlenOfName=parsedFile[1]
    definedInstructions=[]
    definedNames=[]
    for line in lines:
        if line[0] == LineType.SPACE:
            definedInstructions.append('')
            continue
        if line[0] == LineType.COMMENT:
            definedInstructions.append(line[1])
            continue
        definedInstructions.append(genInsField(line, maxlenOfName))
        definedNames.append(genInsNameAssignStmt(line, maxlenOfName))
    return builtCode(definedInstructions, definedNames)

def spaces(name, maxlen):
    return ' ' * (maxlen-len(name))

def genInsField(line, maxlen):
   sp=spaces(line[1], maxlen) 
   return "\t" + FORMAT_INSTRUCTION_FIELD.format(line[1], sp, line[3])

def genInsNameAssignStmt(line, maxlen):
   sp=spaces(line[1], maxlen) 
   return "\t\t" + FORMAT_INSTRUCTION_NAME_FIELD.format(line[1], sp, line[2])

def builtCode(definedInstructions, definedNames):
    insFieldsStr   = '\n'.join(definedInstructions)
    nameAssStmtStr = '\n'.join(definedNames)
    n = len(definedNames)
    code = (
        f'{CODE_PACKAGE}'
        f'{CODE_CLASS}\n'
        f'{insFieldsStr}\n'
        f'\n\t{FORMAT_INSTRUCTION_NUMBER_FIELD.format(n)}\n'
        f'\n\t{FORMAT_INSTRUCTION_NAME_ARRAY_FIELD.format(n)}'
        '\n\tstatic {\n'
        f'{nameAssStmtStr}'
        '\n\t}\n\n'
        f'{CODE_NAME_GETTER}\n'
        '}'
    )
    return code

if __name__ == '__main__':
    print(genJavaCode(parseFile('./ins.txt')))
