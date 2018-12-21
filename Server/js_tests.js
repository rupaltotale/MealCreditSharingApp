const nodemailer = require('nodemailer');

let transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 587,
    secure: false,
    requireTLS: true,
    auth: {
        user: 'mealcreditverifier@gmail.com',
        pass: 'paulhatalsky1928'
    }
});

let mailOptions = {
    from: 'mealcreditverifier@gmail.com',
    to: 'benaglossner@gmail.com',
    subject: 'Test',
    text: 'Hello World!'
};

transporter.sendMail(mailOptions, (error, info) => {
    if (error) {
        return console.log(error.message);
    }
    console.log('success');
});