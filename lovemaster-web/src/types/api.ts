export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn?: number;
}

export interface UserResponse {
  id: number;
  username: string;
  nickname: string;
  avatar?: string | null;
  phone?: string | null;
  email?: string | null;
  gender?: number | null;
  pairCode: string;
  partnerId?: number | null;
  pairedAt?: string | null;
}

export interface PartnerResponse {
  id: number;
  username: string;
  nickname: string;
  avatar?: string | null;
  gender?: number | null;
}

export interface PairingResponse {
  paired: boolean;
  coupleId?: number | null;
  pairCode: string;
  partner?: PartnerResponse | null;
  pairedAt?: string | null;
}

export interface AnniversaryResponse {
  id: number;
  creatorId: number;
  coupleId?: number | null;
  title: string;
  date: string;
  type: string;
  visibility: Visibility;
  remindDays: number[];
  surpriseMode: boolean;
  remark?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PreferenceResponse {
  id: number;
  creatorId: number;
  coupleId?: number | null;
  target: PreferenceTarget;
  category: string;
  content: string;
  visibility: Visibility;
  tags: string[];
  remark?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DateIdeaResponse {
  id: number;
  title: string;
  budgetLevel: string;
  durationLevel: string;
  scene: string;
  interestTags: string[];
  steps: string[];
  tips?: string | null;
  matchScore?: number;
}

export interface MemoryResponse {
  id: number;
  creatorId: number;
  coupleId?: number | null;
  title: string;
  memoryDate: string;
  location?: string | null;
  content?: string | null;
  imageUrl?: string | null;
  visibility: Visibility;
  tags: string[];
  remark?: string | null;
}

export interface AiAdviceResponse {
  advice: string;
  messageTemplate: string;
  riskLevel: 'low' | 'medium' | 'high';
  reminder: string;
}

export type Visibility = 'PRIVATE' | 'COUPLE';
export type PreferenceTarget = 'SELF' | 'PARTNER_OBSERVED';

export class ApiBusinessError extends Error {
  code: number;
  status?: number;

  constructor(message: string, code: number, status?: number) {
    super(message);
    this.name = 'ApiBusinessError';
    this.code = code;
    this.status = status;
  }
}
