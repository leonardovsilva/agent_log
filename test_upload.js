const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

async function testUpload() {
    try {
        const form = new FormData();
        const filePath = path.join(__dirname, 'java_logs.xlsx');

        if (!fs.existsSync(filePath)) {
            console.error("File not found:", filePath);
            return;
        }

        form.append('file', fs.createReadStream(filePath));

        console.log('Uploading file...');
        const response = await axios.post('http://localhost:3000/api/analyze-file', form, {
            headers: {
                ...form.getHeaders()
            }
        });

        console.log('Status:', response.status);
        console.log('Response Data:', JSON.stringify(response.data, null, 2));

        if (response.data.report && response.data.report.charts) {
            console.log('✅ Charts received:', response.data.report.charts.length);
            response.data.report.charts.forEach(c => console.log(`   - Type: ${c.type}, Title: ${c.title}`));
        } else {
            console.error('❌ No charts found in response.');
        }

    } catch (error) {
        if (error.response) {
            console.error('Error Response:', error.response.status, error.response.data);
        } else {
            console.error('Error:', error.message);
        }
    }
}

testUpload();
