/*******************************************************************************
* Copyright (c) 2006, 2010 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.lrparser.gcc;

public interface GCCSizeofExpressionParsersym {
    public final static int
      TK_auto = 30,
      TK_break = 38,
      TK_case = 39,
      TK_char = 51,
      TK_const = 24,
      TK_continue = 40,
      TK_default = 41,
      TK_do = 42,
      TK_double = 52,
      TK_else = 98,
      TK_enum = 63,
      TK_extern = 31,
      TK_float = 53,
      TK_for = 43,
      TK_goto = 44,
      TK_if = 45,
      TK_inline = 32,
      TK_int = 54,
      TK_long = 55,
      TK_register = 33,
      TK_restrict = 26,
      TK_return = 46,
      TK_short = 56,
      TK_signed = 57,
      TK_sizeof = 17,
      TK_static = 28,
      TK_struct = 64,
      TK_switch = 47,
      TK_typedef = 34,
      TK_union = 65,
      TK_unsigned = 58,
      TK_void = 59,
      TK_volatile = 25,
      TK_while = 35,
      TK__Bool = 60,
      TK__Complex = 61,
      TK__Imaginary = 62,
      TK_integer = 18,
      TK_floating = 19,
      TK_charconst = 20,
      TK_stringlit = 11,
      TK_identifier = 1,
      TK_Completion = 5,
      TK_EndOfCompletion = 3,
      TK_Invalid = 100,
      TK_LeftBracket = 36,
      TK_LeftParen = 2,
      TK_LeftBrace = 14,
      TK_Dot = 70,
      TK_Arrow = 85,
      TK_PlusPlus = 15,
      TK_MinusMinus = 16,
      TK_And = 12,
      TK_Star = 6,
      TK_Plus = 9,
      TK_Minus = 10,
      TK_Tilde = 21,
      TK_Bang = 22,
      TK_Slash = 71,
      TK_Percent = 72,
      TK_RightShift = 66,
      TK_LeftShift = 67,
      TK_LT = 73,
      TK_GT = 74,
      TK_LE = 75,
      TK_GE = 76,
      TK_EQ = 80,
      TK_NE = 81,
      TK_Caret = 82,
      TK_Or = 83,
      TK_AndAnd = 84,
      TK_OrOr = 86,
      TK_Question = 87,
      TK_Colon = 48,
      TK_DotDotDot = 68,
      TK_Assign = 69,
      TK_StarAssign = 88,
      TK_SlashAssign = 89,
      TK_PercentAssign = 90,
      TK_PlusAssign = 91,
      TK_MinusAssign = 92,
      TK_RightShiftAssign = 93,
      TK_LeftShiftAssign = 94,
      TK_AndAssign = 95,
      TK_CaretAssign = 96,
      TK_OrAssign = 97,
      TK_Comma = 49,
      TK_RightBracket = 77,
      TK_RightParen = 37,
      TK_RightBrace = 50,
      TK_SemiColon = 27,
      TK_typeof = 13,
      TK___alignof__ = 23,
      TK___attribute__ = 7,
      TK___declspec = 8,
      TK_MAX = 78,
      TK_MIN = 79,
      TK_asm = 4,
      TK_ERROR_TOKEN = 29,
      TK_EOF_TOKEN = 99;

      public final static String orderedTerminalSymbols[] = {
                 "",
                 "identifier",
                 "LeftParen",
                 "EndOfCompletion",
                 "asm",
                 "Completion",
                 "Star",
                 "__attribute__",
                 "__declspec",
                 "Plus",
                 "Minus",
                 "stringlit",
                 "And",
                 "typeof",
                 "LeftBrace",
                 "PlusPlus",
                 "MinusMinus",
                 "sizeof",
                 "integer",
                 "floating",
                 "charconst",
                 "Tilde",
                 "Bang",
                 "__alignof__",
                 "const",
                 "volatile",
                 "restrict",
                 "SemiColon",
                 "static",
                 "ERROR_TOKEN",
                 "auto",
                 "extern",
                 "inline",
                 "register",
                 "typedef",
                 "while",
                 "LeftBracket",
                 "RightParen",
                 "break",
                 "case",
                 "continue",
                 "default",
                 "do",
                 "for",
                 "goto",
                 "if",
                 "return",
                 "switch",
                 "Colon",
                 "Comma",
                 "RightBrace",
                 "char",
                 "double",
                 "float",
                 "int",
                 "long",
                 "short",
                 "signed",
                 "unsigned",
                 "void",
                 "_Bool",
                 "_Complex",
                 "_Imaginary",
                 "enum",
                 "struct",
                 "union",
                 "RightShift",
                 "LeftShift",
                 "DotDotDot",
                 "Assign",
                 "Dot",
                 "Slash",
                 "Percent",
                 "LT",
                 "GT",
                 "LE",
                 "GE",
                 "RightBracket",
                 "MAX",
                 "MIN",
                 "EQ",
                 "NE",
                 "Caret",
                 "Or",
                 "AndAnd",
                 "Arrow",
                 "OrOr",
                 "Question",
                 "StarAssign",
                 "SlashAssign",
                 "PercentAssign",
                 "PlusAssign",
                 "MinusAssign",
                 "RightShiftAssign",
                 "LeftShiftAssign",
                 "AndAssign",
                 "CaretAssign",
                 "OrAssign",
                 "else",
                 "EOF_TOKEN",
                 "Invalid"
             };

    public final static boolean isValidForParser = true;
}
