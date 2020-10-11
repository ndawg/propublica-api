About
---------
This library provides a developer friendly interface for the ProPublica Congressional API. However, it does not just wrap it. Instead, it attempts to improve the API's responses. For this reason
, not all parts are present.

Completion Status
---------

### Members
- [x] Lists of Members
- [x] Get a Specific Member
- [ ] Get New Members
- [ ] Get Current Members by State/District
- [ ] Get Members Leaving Office
- [ ] Get a Specific Member's Vote Positions
- [ ] Compare Two Members Vote Positions
- [ ] Compare Two Members Bill Sponsorships
- [ ] Get Bills Cosponsored by a Specific Member

##### Office Expenses 
- [ ] Get Quarterly Office Expenses by a Specific House Member
- [ ] Get Quarterly Office Expenses by Category for a specific House Member
- [ ] Get Quarterly Office Expenses for a Specified Category

### Bills

- [x] Search Bills
- [x] Get Recent Bills
- [x] Get Recent Bills by a Specific Member
- [x] Get Recent Bills by a Specific Subject
- [x] Get Upcoming Bills
- [x] Get a Specific Bill
- [x] Get Amendments for a Specific Bill
- [x] Get Subjects for a Specific Bill
- [x] Get Related Bills for a Specific Bill
- [x] Get a Specific Bill Subject
- [x] Get Cosponsors for a Specific Bill

### Votes

- [x] Get Recent Votes
- [x] Get a Specific Roll Call Vote
- [ ] Get Votes by Type
- [ ] Get Votes by Date
- [x] Get Senate Nomination Votes

##### Personal Explanations

- [ ] Get Recent Personal Explanations
- [ ] Get Recent Personal Explanation Votes
- [ ] Get Recent Personal Explanation Votes by Category
- [ ] Get Recent Personal Explanations by a Specific Member
- [ ] Get Recent PErsonal Explanation Votes by a Specific Member
- [ ] Get Recent Personal Explanation Votes by a Specific Member by Category

### Statements

- [x] Get Recent Congressional Statements
- [ ] Get Congressional Statements by Date
- [ ] Get Congressional Statements by Search Term
- [ ] Get Statement Subjects
- [ ] Get Congressional Statements by Subject
- [ ] Get Congressional Statements by Member
- [ ] Get Congressional Statements by Bill 

##### Congressional Committee Statements

- [ ] Get Recent Congressional Committee Statements
- [ ] Get Congressional Committee Statements by Committee
- [ ] Get Congressional Statements by Committee
- [ ] Get Congressional Committee Statements by Search Term

### Committees

- [ ] List of Committees
- [ ] Get a Specific Committee
- [ ] Get Recent Committee Hearings
- [ ] Get Hearings for a Specific Committee
- [ ] Get a Specific Subcommittee

##### Official Communications

- [ ] Get Recent Official Communications
- [ ] Get Recent Official Communications by Category
- [ ] Get Recent Official Communications by Date
- [ ] Get Recent Official Communications by Chamber

### Other Endpoints

##### Nominations

- [ ] Get Recent Nominations by Category
- [ ] Get a Specific Nomination
- [ ] Get Nominess by State

##### Floor Actions

- [ ] Get Recent House and Senate Floor Actions
- [ ] Get House and Senate Floor Actions by Date

##### Lobbying

- [ ] Get Recent Lobbying Representation filings 
- [ ] Search Lobbying Representation filings
- [ ] Get a Specific Lobbying Representation filing

##### Other Responses

- [ ] Get State Party Counts


Test Types
---------

mocked: tests that rely on mocked responses from the raw api, and require no network

raw: tests that directly use the raw api

live: tests that rely on both the raw api and the pipeline for transformation

exhaustive: tests that exhaust endpoints to ensure complete coverage, can use mocked responses or live api
