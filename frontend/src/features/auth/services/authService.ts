import { apiRequest } from "../../../services/api/apiClient";

export type LoginPayload = {
  tenantSlug: string;
  email: string;
  senha: string;
};

export type AuthResponse = {
  token: string;
  tokenType: string;
};

export async function autenticar(
  payload: LoginPayload,
): Promise<AuthResponse> {
  const response = await apiRequest("/api/auth/login", {
    method: "POST",
    body: payload,
  });

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      throw new Error("Empresa, e-mail ou senha inválidos.");
    }

    if (response.status === 400) {
      throw new Error("Confira os dados informados e tente novamente.");
    }

    throw new Error("Não foi possível entrar. Tente novamente.");
  }

  const data: AuthResponse = await response.json();

  if (!data.token) {
    throw new Error("O servidor não retornou um token de acesso válido.");
  }

  return {
    token: data.token,
    tokenType: data.tokenType || "Bearer",
  };
}
