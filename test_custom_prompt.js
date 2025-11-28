const fs = require('fs');
const path = require('path');
const axios = require('axios');
const FormData = require('form-data');

async function testUpload() {
    try {
        const form = new FormData();
        const filePath = path.join(__dirname, 'test_logs.txt');
        form.append('file', fs.createReadStream(filePath));
        form.append('customPrompt', 'Ignore os logs de INFO, foque apenas nos ERROS.');

        console.log('Uploading test_logs.txt with custom prompt...');

        const response = await axios.post('http://localhost:3000/api/analyze-file', form, {
            headers: {
                ...form.getHeaders()
            }
        });

        console.log('Response status:', response.status);
        console.log('Response data:', JSON.stringify(response.data, null, 2));

        if (response.data.report && response.data.report.summary) {
            console.log('TEST PASSED: Report generated.');
        } else {
            console.log('TEST FAILED: No report generated.');
        }

    } catch (error) {
        console.error('TEST FAILED:', error.message);
        if (error.response) {
            console.error('Server response:', error.response.data);
        }
    }
}

testUpload();
