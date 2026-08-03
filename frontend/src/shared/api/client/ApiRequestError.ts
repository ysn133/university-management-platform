export class ApiRequestError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = "ApiRequestError";
  }
}

export function apiRequestError(response: Response, error: unknown): ApiRequestError {
  if (typeof error === "object" && error !== null && "message" in error) {
    const message = (error as { message?: unknown }).message;
    if (typeof message === "string" && message.length > 0) {
      return new ApiRequestError(message, response.status);
    }
  }

  if (response.status === 401) {
    return new ApiRequestError("The email or password is incorrect.", response.status);
  }

  if (response.status === 403) {
    return new ApiRequestError("This account cannot access the requested resource.", response.status);
  }

  return new ApiRequestError("The request could not be completed.", response.status);
}
