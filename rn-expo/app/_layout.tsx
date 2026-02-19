import { Stack } from "expo-router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StatusBar } from "expo-status-bar";

const queryClient = new QueryClient();

export default function RootLayout() {
  return (
    <QueryClientProvider client={queryClient}>
      <Stack>
        <Stack.Screen name="index" options={{ title: "RN-Expo" }} />
        <Stack.Screen name="movie/[id]" options={{ title: "Movie Detail" }} />
      </Stack>
      <StatusBar style="auto" />
    </QueryClientProvider>
  );
}
