// Check if we're on login or register page
const isLoginPage = window.location.pathname.includes('login.html');
const isRegisterPage = window.location.pathname.includes('register.html');

// Check authentication status on page load
window.addEventListener('DOMContentLoaded', async () => {
    if (isLoginPage || isRegisterPage) {
        const response = await fetch('/api/auth/check');
        const data = await response.json();

        if (data.authenticated) {
            window.location.href = '/budgets.html';
        }
    }
});

// Login form handler
if (isLoginPage) {
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const errorMessage = document.getElementById('errorMessage');

        errorMessage.style.display = 'none';

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password }),
                credentials: 'include'
            });

            const data = await response.json();

            if (data.success) {
                window.location.href = '/budgets.html';
            } else {
                errorMessage.textContent = data.message || 'Giriş başarısız!';
                errorMessage.style.display = 'block';
            }
        } catch (error) {
            errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
            errorMessage.style.display = 'block';
        }
    });
}

// Register form handler
if (isRegisterPage) {
    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const errorMessage = document.getElementById('errorMessage');
        const successMessage = document.getElementById('successMessage');

        errorMessage.style.display = 'none';
        successMessage.style.display = 'none';

        if (password !== confirmPassword) {
            errorMessage.textContent = 'Şifreler eşleşmiyor!';
            errorMessage.style.display = 'block';
            return;
        }

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password }),
                credentials: 'include'
            });

            const data = await response.json();

            if (data.success) {
                successMessage.textContent = 'Kayıt başarılı! Giriş sayfasına yönlendiriliyorsunuz...';
                successMessage.style.display = 'block';
                setTimeout(() => {
                    window.location.href = '/login.html';
                }, 2000);
            } else {
                errorMessage.textContent = data.message || 'Kayıt başarısız!';
                errorMessage.style.display = 'block';
            }
        } catch (error) {
            errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
            errorMessage.style.display = 'block';
        }
    });
}