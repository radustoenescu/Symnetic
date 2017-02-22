grammar P4;

p4 
: 'control' control_fn_name control_block 
;

control_fn_name
: m1 = Id {System.out.println("CTRL function " + $m1.text);}
;

control_block
: '{' (control_statement)* '}'
;

control_statement
: apply_table_call 
    | apply_and_select_block 
    | if_else_statement 
    | control_fn_name '('')'
;

apply_table_call 
: 'apply' '(' table_name ')'  ';'
;

table_name
: m = Id {System.out.println("Table "+$m.text);}
;

apply_and_select_block 
: 'apply' '(' table_name ')' '{' (case_list)?  '}'
    ; 

case_list 
: action_case + 
| hit_miss_case +
    ;

action_case 
: action_or_default control_block
    ;

action_or_default 
: action_name | 'default'
    ;

action_name 
: Id
    ;

hit_miss_case 
: hit_or_miss control_block
    ;

hit_or_miss 
: 'hit' | 'miss'
    ;

if_else_statement 
: 'if' '(' bool_expr ')' control_block (else_block)? {System.out.println("IF statement");}
    ;

else_block 
: 'else' control_block {System.out.println("ELSE");}
| 'else' if_else_statement
    ;

bool_expr 
: 'valid' '(' header_ref ')' 
| bool_expr bool_op bool_expr 
| 'not' bool_expr 
| '(' bool_expr ')' 
| exp rel_op exp 
| 'true' 
| 'false'
    ;

exp 
: exp bin_op exp 
| un_op exp 
| field_ref 
| value 
| '(' exp ')'
    ;

value
    : const_value
    ;

bin_op 
: '+' 
| '*' 
| '-' 
| '<<' 
| '>>' 
| '&' 
| '|' 
| '^' 
    ;

un_op 
: '~' 
| '-'
    ;

bool_op 
: 'or' 
| 'and'
    ;

rel_op 
: '>' 
| '>=' 
| '==' 
| '<=' 
| '<' 
| '!='
    ;

header_ref 
: instance_name 
| instance_name '[' index ']'
    ;

instance_name
    : Id
    ;

index 
: const_value 
| 'last'
    ;


field_ref 
: header_ref '.' field_name
    ;

field_name
    : Id
    ;


const_value
    : Number
    ;


/* A number: can be an integer value, or a decimal value */
Number
:    ('0'..'9')+ ('.' ('0'..'9')+)?
;

Id 
:    (('a'..'z')|('A'..'Z')|'_')(('a'..'z')|('A'..'Z')|'_'|('0'..'9'))* 
;


/* We're going to ignore all white space characters */
WS  
    :   (' ' | '\t' | '\r'| '\n') -> skip
    ;


Comment
    : '//' ~( '\r' | '\n' )* -> skip
    ;

comment
    : Comment
    ;

