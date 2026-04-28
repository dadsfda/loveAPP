export function parseTagsInput(input: string) {
  return Array.from(
    new Set(
      input
        .split(/[\s,，、]+/)
        .map((tag) => tag.trim())
        .filter(Boolean)
    )
  );
}
