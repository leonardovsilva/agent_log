const xlsx = require('xlsx');

const data = [
    { Date: '2023-01-01', Latency: 120, Error: 'None' },
    { Date: '2023-01-02', Latency: 150, Error: 'Timeout' },
    { Date: '2023-01-03', Latency: 200, Error: '500' }
];

const wb = xlsx.utils.book_new();
const ws = xlsx.utils.json_to_sheet(data);
xlsx.utils.book_append_sheet(wb, ws, "TestSheet");
xlsx.writeFile(wb, "test.xlsx");
console.log("test.xlsx created");
