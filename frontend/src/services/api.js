const API_BASE_URL = 'http://localhost:8080';

export function getAuthToken() {
  return localStorage.getItem('campusos_token');
}

export function setAuthToken(token) {
  localStorage.setItem('campusos_token', token);
}

export function clearAuthToken() {
  localStorage.removeItem('campusos_token');
}

export function getCurrentUserFromToken() {
  const token = getAuthToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1] || ''));
    return {
      email: payload.sub,
      name: payload.sub,
      role: payload.role || 'STUDENT'
    };
  } catch (error) {
    return null;
  }
}

export const apiClient = {
  async get(url) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}${url}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Request failed');
    }

    return response.json();
  },

  async post(url, data) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Request failed');
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    return null;
  },

  async put(url, data) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Request failed');
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    return null;
  },

  async delete(url) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'DELETE',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Request failed');
    }

    return null;
  }
};
