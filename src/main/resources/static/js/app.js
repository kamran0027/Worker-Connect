/* WorkerConnect – Global JS */

// Auto-dismiss alerts after 5 s
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert-dismissible').forEach(el => {
    setTimeout(() => {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(el);
      if (bsAlert) bsAlert.close();
    }, 5000);
  });

  // Confirm delete/cancel actions
  document.querySelectorAll('[data-confirm]').forEach(btn => {
    btn.addEventListener('click', e => {
      if (!confirm(btn.dataset.confirm || 'Are you sure?')) e.preventDefault();
    });
  });

  // Preview profile image before upload
  const imgInput = document.getElementById('profileImageInput');
  if (imgInput) {
    imgInput.addEventListener('change', function () {
      const reader = new FileReader();
      reader.onload = e => {
        const preview = document.getElementById('profilePreview');
        if (preview) preview.src = e.target.result;
      };
      if (this.files[0]) reader.readAsDataURL(this.files[0]);
    });
  }

  // Star rating
  document.querySelectorAll('.star-label').forEach(star => {
    star.addEventListener('mouseover', function () {
      const val = parseInt(this.dataset.value);
      document.querySelectorAll('.star-label').forEach((s, i) => {
        s.style.color = i < val ? '#f59e0b' : '#d1d5db';
      });
    });
  });
});

// Toggle password visibility
function togglePassword(inputId, iconId) {
  const input = document.getElementById(inputId);
  const icon  = document.getElementById(iconId);
  if (input.type === 'password') {
    input.type = 'text';
    icon.classList.replace('fa-eye', 'fa-eye-slash');
  } else {
    input.type = 'password';
    icon.classList.replace('fa-eye-slash', 'fa-eye');
  }
}
