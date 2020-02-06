type Person record {|
   string firstName;
   string lastName;
   int age;
|};


function testSimpleSelectQueryWithSimpleVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariable() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, age: a } in personList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   age: a
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName, lastName, age } in personList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   age: age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithWhereClause() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            where person.age >= 30
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };
    return  outputPersonList;
}

function testQueryExpressionForPrimitiveType() returns int[]{

    int[] intList = [12, 13, 14, 15, 20, 21, 25];

    int[] outputIntList =
            from var value in intList
            where value > 20
            select value;

    return  outputIntList;
}

function testQueryExpressionWithSelectExpression() returns string[]{

    int[] intList = [1, 2, 3];

    string[] stringOutput =
            from var value in intList
            select value.toString();

    return  stringOutput;
}

function testFilteringNullElements() returns Person[] {

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};

    Person?[] personList = [p1,  (), p2];

    Person[] outputPersonList =
                     from var person in personList
                     where (person is Person)
                     select {
                         firstName: person.firstName,
                         lastName: person.lastName,
                         age: person.age
                         };
    return outputPersonList;
}

function testMapWithArity () returns string[] {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    string[] val = from var v in m
                   where <string> v == "1A"
                   select v;
    return val;
}

function testJSONArrayWithArity() returns string[] {
    json[] jdata = [{ name : "bob", age : 10}, { name : "tom", age : 16}];
    string[] val = from var v in jdata
                   select v.name;
    return val;
}

function testArrayWithTuple() returns string[] {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];
    string[] val = from var [i, v] in arr
                   where i == 3
                   select v;
    return val;
}
