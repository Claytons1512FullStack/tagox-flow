const TOKEN_KEY = 'tagox_flow_token'
const TOKEN_TYPE_KEY = 'tagox_flow_token_type'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function saveSession(token: string, tokenType: string): void {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_TYPE_KEY, tokenType || 'Bearer')
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_TYPE_KEY)
}