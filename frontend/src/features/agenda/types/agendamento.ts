export type AgendamentoStatus =
  | "SOLICITADO"
  | "CONFIRMADO"
  | "CANCELADO"
  | "CONCLUIDO";

export type Agendamento = {
  id: string;
  profissionalId: string;
  pacienteId: string;
  inicio: string;
  fim: string;
  status: AgendamentoStatus;
  observacoes: string | null;
};
