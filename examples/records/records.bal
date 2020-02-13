import ballerina/io;

// Define an open record type named `Student`. The `{` and `}` delimiters indicate that in addition to the defined fields,
// this record type allows additional fields with `anydata` values.
// The descriptor `record { }` is equivalent to `record {| anydata...; |}`.
type Student record {
    string name;
    int age;
    Grades grades;
};

// Define a closed record type named `Address`. The `{|` and `|}` delimiters indicate that this record type
// allows mapping values, which contain only the described fields.
type Address record {|
    string city;
    string country;
|};

// Define an open record type named `Grades`. Although it is defined using the `{|` and `|}` delimiters, it has
// an `int` rest field as well. Therefore, this is an open record type.
type Grades record {|
    int maths;
    int physics;
    int chemistry;
    // This is a rest field of the type`int`. All additional fields should be of the type or a subtype of the rest field.
    int...;
|};


public function main() {
    // This creates a `Student` record. Since all the fields are required and none of the fields
    // have explicit default values assigned to them, values must be specified for all the fields
    // when creating the record.
    Student john = {
        name: "John Doe",
        age: 17,
        grades: {
            maths: 80,
            physics: 75,
            chemistry: 65
        }
    };
    io:println(john);

    // This is an example of field-based access of record fields. The return type of this expression is the
    // type of the field. Field access is only allowed for required fields in a record.
    io:println(john.name);

    // This is an example of member access of record fields. Where the type of the field is `T`, the type of
    // this expression is `T` if the field is a required field or has a default value. If the field is an optional
    // field or a rest field, the type of this expression is `T?`.
    // If it is a closed record, accessing an undefined key will result in a compilation error.
    io:println(john["name"]);

    // This fetches a field of a nested record.
    io:println(john.grades.maths);

    Student peter = {
        name: "Peter",
        age: 19,
        grades: {
            maths: 40,
            physics: 35,
            chemistry: 35
        }
    };
    // This modifies the value of the `age` field.
    // Field access is allowed with assignment only for fields defined in the record type descriptor.
    peter.age = 16;

    io:println(peter);
    io:println(john);

    // Member access can be used to assign to fields that are not defined in the record type descriptor.
    // An attempt to add additional fields to a closed record results in compile errors.
    peter["address"] = <Address>{city: "Colombo", country: "Sri Lanka"};
    io:println(peter);

    // Create a `Grades` record adding additional fields for the `int`-typed rest field.
    // The `english` field is not specified in the record, but is allowed since `Grades` is an
    // open record with an `int`-typed rest field.
    // Keys for such field should either be `string` literals or expressions (i.e., they
    // cannot be identifiers).
    Grades grades = {maths: 80, physics: 75, chemistry: 65, "english": 90};
    io:println(grades);

    // Similarly, only member access can be used to access the fields that are possibly
    // added for the rest field. An `int` value is returned if the field is present in the
    // record, else `()` is returned.
    int? english = grades["english"];
    io:println(english);
}
