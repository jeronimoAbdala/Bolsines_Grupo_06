import axios from 'axios';
import { env } from '@/config/env';

export const api = axios.create({
  baseURL: env.apiUrl,
});

api.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error),
);
