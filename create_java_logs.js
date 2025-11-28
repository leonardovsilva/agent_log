const xlsx = require('xlsx');
const fs = require('fs');
const path = require('path');

const data = [
    { timestamp: '2023-10-27T10:00:01Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 120, url: '/api/users' },
    { timestamp: '2023-10-27T10:00:02Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 150, url: '/api/products' },
    { timestamp: '2023-10-27T10:00:05Z', level: 'ERROR', message: 'Database connection failed', http_status: 500, response_time: 500, url: '/api/checkout' },
    { timestamp: '2023-10-27T10:00:10Z', level: 'WARN', message: 'Resource not found', http_status: 404, response_time: 50, url: '/api/unknown' },
    { timestamp: '2023-10-27T10:00:15Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 200, url: '/api/users' },
    { timestamp: '2023-10-27T10:00:20Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 180, url: '/api/products' },
    { timestamp: '2023-10-27T10:00:25Z', level: 'ERROR', message: 'Payment gateway timeout', http_status: 503, response_time: 5000, url: '/api/checkout' },
    { timestamp: '2023-10-27T10:00:30Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 130, url: '/api/login' },
    { timestamp: '2023-10-27T10:00:35Z', level: 'INFO', message: 'Request processed', http_status: 200, response_time: 140, url: '/api/login' },
    { timestamp: '2023-10-27T10:00:40Z', level: 'WARN', message: 'Slow query detected', http_status: 200, response_time: 1200, url: '/api/reports' }
];

const wb = xlsx.utils.book_new();
const ws = xlsx.utils.json_to_sheet(data);
xlsx.utils.book_append_sheet(wb, ws, 'Logs');

const filePath = path.join(__dirname, 'java_logs.xlsx');
xlsx.writeFile(wb, filePath);

console.log(`Test file created at: ${filePath}`);
