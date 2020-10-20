<template>
  <v-app>
    <v-app-bar app color="primary" dark>
      <div class="d-flex align-center">
        <v-img
          alt="IBM Cloud Logo"
          class="shrink mr-2"
          contain
          src="https://cdn.vuetifyjs.com/images/logos/vuetify-logo-dark.png"
          transition="scale-transition"
          width="40"
        />
      </div>
      <v-toolbar-title>EDA- Order Manager - v 0.6.0 </v-toolbar-title>

      <v-spacer></v-spacer>
      <v-btn to="/" text>
          <v-icon left>mdi-home</v-icon>
          Home
        </v-btn>
        <v-btn to="/orders" text>
          <v-icon left>mdi-store</v-icon>
          Orders
        </v-btn>
      <v-btn
        href="https://github.com/ibm-cloud-architecture/refarch-kc-order-ms"
        target="_blank"
        text
      >
        <span class="mr-2">Git</span>
        <v-icon>mdi-git</v-icon>
      </v-btn>
    </v-app-bar>
    <v-content>
      <v-container>
        <router-view />
      </v-container>
    </v-content>
  </v-app>
</template>

<script>

export default {
  name: "App",
  data: () => ({
    //
  }),
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
