import { apiRequest } from "../../../services/api/apiClient";
import type { Patient } from "../types/patient";

export type PatientPayload = {
  nome: string;
  cpf: string | null;
  dataNascimento: string | null;
  telefone: string | null;
  email: string | null;
};

export async function readError(response: Response): Promise<string> {
  const body = await response.text();

  if (body) {
    try {
      const data: unknown = JSON.parse(body);

      if (typeof data === "object" && data !== null) {
        const details = data as {
          message?: unknown;
          detail?: unknown;
          error?: unknown;
        };

        const message =
          details.message ?? details.detail ?? details.error;

        if (typeof message === "string" && message.trim()) {
          return message;
        }
      }
    } catch {
      // A resposta não está em JSON.
    }
  }

  if (response.status === 400) {
    return "Confira os dados informados. Verifique o CPF e o e-mail.";
  }

  if (response.status === 403) {
    return "Seu perfil não tem permissão para acessar ou alterar pacientes.";
  }

  if (response.status === 404) {
    return "O paciente solicitado não foi encontrado.";
  }

  return "Não foi possível concluir a operação. Tente novamente.";
}

export function listarPacientes(token: string): Promise<Response> {
  return apiRequest("/api/patients", {
    token,
  });
}

export function salvarPacienteNaApi(
  token: string,
  pacienteId: string | null,
  payload: PatientPayload,
): Promise<Response> {
  const editando = pacienteId !== null;

  const endpoint = editando
    ? `/api/patients/${encodeURIComponent(pacienteId)}`
    : "/api/patients";

  return apiRequest(endpoint, {
    token,
    method: editando ? "PUT" : "POST",
    body: payload,
  });
}

export async function interpretarListaPacientes(
  response: Response,
): Promise<Patient[]> {
  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return (await response.json()) as Patient[];
}
